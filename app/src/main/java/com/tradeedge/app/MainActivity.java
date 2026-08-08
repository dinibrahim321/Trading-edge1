package com.tradeedge.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    LinearLayout root, content;
    ArrayList<Trade> trades = new ArrayList<>();
    SharedPreferences prefs;

    int green = Color.rgb(76, 175, 80);
    int red = Color.rgb(244, 67, 54);
    int bg = Color.rgb(16, 19, 24);
    int card = Color.rgb(27, 32, 40);
    int text = Color.WHITE;
    int muted = Color.rgb(170, 178, 190);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("tradeedge", MODE_PRIVATE);
        loadTrades();

        showDashboard();
    }

    // -----------------------------
    // BASIC UI
    // -----------------------------

    TextView tv(String value, int size, int color) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setPadding(18, 14, 18, 14);
        return v;
    }

    Button btn(String value) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setAllCaps(false);
        b.setBackgroundColor(card);
        b.setPadding(10, 8, 10, 8);
        return b;
    }

    EditText input(String hint) {
        EditText e = new EditText(this);

        e.setHint(hint);
        e.setHintTextColor(muted);
        e.setTextColor(text);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(18, 12, 18, 12);

        return e;
    }

    void base(String title) {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);

        TextView titleView = tv(title, 22, text);
        titleView.setTypeface(null, 1);
        titleView.setBackgroundColor(bg);

        root.addView(
                titleView,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(10, 5, 10, 10);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(content);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // Bottom navigation

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(card);

        Button dashboard = btn("Dashboard");
        Button addTrade = btn("+ Trade");
        Button journal = btn("Journal");
        Button checklist = btn("Checklist");

        nav.addView(
                dashboard,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        nav.addView(
                addTrade,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        nav.addView(
                journal,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        nav.addView(
                checklist,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        dashboard.setOnClickListener(v -> showDashboard());
        addTrade.setOnClickListener(v -> showAdd());
        journal.setOnClickListener(v -> showJournal());
        checklist.setOnClickListener(v -> showChecklist());

        root.addView(nav);

        setContentView(root);
    }

    // -----------------------------
    // DASHBOARD
    // -----------------------------

    void showDashboard() {

        base("TradeEdge");

        content.addView(
                tv("Trading Dashboard", 26, text)
        );

        int wins = 0;
        int losses = 0;
        int breakeven = 0;

        double totalR = 0;

        for (Trade trade : trades) {

            if (trade.result.equalsIgnoreCase("Win")) {
                wins++;
            } else if (trade.result.equalsIgnoreCase("Loss")) {
                losses++;
            } else {
                breakeven++;
            }

            totalR += trade.r;
        }

        int totalTrades = trades.size();

        double winRate = 0;

        if (totalTrades > 0) {
            winRate = (wins * 100.0) / totalTrades;
        }

        double averageR = 0;

        if (totalTrades > 0) {
            averageR = totalR / totalTrades;
        }

        content.addView(
                tv(
                        "Trades: " + totalTrades +
                                "    Win Rate: " +
                                String.format(Locale.US, "%.1f", winRate) +
                                "%",
                        16,
                        muted
                )
        );

        TextView netR = tv(
                "Net R: " +
                        String.format(Locale.US, "%.2f", totalR),
                24,
                totalR >= 0 ? green : red
        );

        netR.setTypeface(null, 1);

        content.addView(netR);

        content.addView(
                tv("Quick Stats", 20, text)
        );

        content.addView(
                tv("Wins: " + wins, 16, text)
        );

        content.addView(
                tv("Losses: " + losses, 16, text)
        );

        content.addView(
                tv("Breakeven: " + breakeven, 16, text)
        );

        content.addView(
                tv(
                        "Average R: " +
                                String.format(Locale.US, "%.2f", averageR),
                        16,
                        text
                )
        );

        content.addView(
                tv("Trading Rules", 20, text)
        );

        content.addView(
                tv(
                        "Focus on following your plan rather than judging yourself by one trade.",
                        14,
                        muted
                )
        );

        content.addView(
                tv("Recent Trades", 20, text)
        );

        if (trades.isEmpty()) {

            content.addView(
                    tv(
                            "No trades yet.\nTap + Trade to record your first setup.",
                            15,
                            muted
                    )
            );

        } else {

            int start = Math.max(0, trades.size() - 5);

            for (int i = start; i < trades.size(); i++) {

                content.addView(
                        tv(
                                trades.get(i).summary(),
                                15,
                                text
                        )
                );
            }
        }
    }

    // -----------------------------
    // ADD TRADE
    // -----------------------------

    void showAdd() {

        base("Add Trade");

        EditText pair = input("Pair — EUR/USD");
        EditText side = input("Direction — Buy / Sell");
        EditText entry = input("Entry Price");
        EditText sl = input("Stop Loss");
        EditText tp = input("Take Profit");
        EditText lots = input("Lot Size");
        EditText result = input("Result — Win / Loss / BE");
        EditText rResult = input("R Result — example: 2 or -1");
        EditText setup = input(
                "Setup — Supply/Demand, BOS, MSS..."
        );
        EditText session = input(
                "Session — London / New York / Asia"
        );
        EditText notes = input(
                "Notes — emotions, mistakes, observations"
        );

        content.addView(pair);
        content.addView(side);
        content.addView(entry);
        content.addView(sl);
        content.addView(tp);
        content.addView(lots);
        content.addView(result);
        content.addView(rResult);
        content.addView(setup);
        content.addView(session);
        content.addView(notes);

        Button save = btn("SAVE TRADE");

        content.addView(
                save,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        save.setOnClickListener(v -> {

            try {

                String pairText =
                        pair.getText().toString().trim();

                String sideText =
                        side.getText().toString().trim();

                String resultText =
                        result.getText().toString().trim();

                String setupText =
                        setup.getText().toString().trim();

                String sessionText =
                        session.getText().toString().trim();

                String notesText =
                        notes.getText().toString().trim();

                double r =
                        Double.parseDouble(
                                rResult.getText()
                                        .toString()
                                        .trim()
                        );

                if (pairText.isEmpty()) {
                    Toast.makeText(
                            this,
                            "Enter a trading pair.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                if (resultText.isEmpty()) {
                    Toast.makeText(
                            this,
                            "Enter the trade result.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                trades.add(
                        new Trade(
                                pairText,
                                sideText,
                                resultText,
                                r,
                                setupText,
                                sessionText,
                                notesText
                        )
                );

                saveTrades();

                Toast.makeText(
                        this,
                        "Trade saved successfully.",
                        Toast.LENGTH_SHORT
                ).show();

                showJournal();

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Enter a valid R result.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // -----------------------------
    // JOURNAL
    // -----------------------------

    void showJournal() {

        base("Trade Journal");

        if (trades.isEmpty()) {

            content.addView(
                    tv(
                            "Your journal is empty.",
                            18,
                            muted
                    )
            );

            return;
        }

        for (int i = trades.size() - 1; i >= 0; i--) {

            Trade trade = trades.get(i);

            LinearLayout box = new LinearLayout(this);

            box.setOrientation(
                    LinearLayout.VERTICAL
            );

            box.setPadding(
                    8,
                    8,
                    8,
                    8
            );

            box.setBackgroundColor(card);

            TextView summary =
                    tv(
                            trade.summary(),
                            17,
                            text
                    );

            summary.setTypeface(null, 1);

            box.addView(summary);

            box.addView(
                    tv(
                            "Setup: " +
                                    trade.setup +
                                    "\nSession: " +
                                    trade.session,
                            14,
                            muted
                    )
            );

            String notesText =
                    trade.notes.isEmpty()
                            ? "No notes."
                            : "Notes: " + trade.notes;

            box.addView(
                    tv(
                            notesText,
                            14,
                            muted
                    )
            );

            Button delete = btn("Delete");

            box.addView(delete);

            final int index = i;

            delete.setOnClickListener(v -> {

                trades.remove(index);

                saveTrades();

                showJournal();
            });

            content.addView(box);

            TextView spacer =
                    tv("", 4, bg);

            content.addView(spacer);
        }
    }

    // -----------------------------
    // CHECKLIST
    // -----------------------------

    void showChecklist() {

        base("Execution Checklist");

        content.addView(
                tv(
                        "Only take trades that satisfy your plan.",
                        20,
                        text
                )
        );

        String[] checklistItems = {

                "HTF market context identified",

                "Higher-timeframe trend is clear",

                "Key Supply/Demand zone marked",

                "Liquidity / Equal Highs / Equal Lows identified",

                "BOS or MSS supports the trade idea",

                "Lower-timeframe confirmation is clear",

                "Entry, Stop Loss and Take Profit are defined",

                "Risk is within my fixed risk limit",

                "Minimum planned R:R is met",

                "No FOMO entry",

                "No revenge trade",

                "I am willing to accept the loss",

                "I will not move my Stop Loss emotionally"

        };

        for (String item : checklistItems) {

            CheckBox checkBox =
                    new CheckBox(this);

            checkBox.setText(item);
            checkBox.setTextColor(text);
            checkBox.setTextSize(15);
            checkBox.setPadding(
                    8,
                    8,
                    8,
                    8
            );

            content.addView(checkBox);
        }

        Button reset =
                btn("RESET CHECKLIST");

        content.addView(reset);

        reset.setOnClickListener(
                v -> showChecklist()
        );
    }

    // -----------------------------
    // SAVE TRADES
    // -----------------------------

    void saveTrades() {

        JSONArray array =
                new JSONArray();

        try {

            for (Trade trade : trades) {

                JSONObject object =
                        new JSONObject();

                object.put(
                        "pair",
                        trade.pair
                );

                object.put(
                        "side",
                        trade.side
                );

                object.put(
                        "result",
                        trade.result
                );

                object.put(
                        "r",
                        trade.r
                );

                object.put(
                        "setup",
                        trade.setup
                );

                object.put(
                        "session",
                        trade.session
                );

                object.put(
                        "notes",
                        trade.notes
                );

                array.put(object);
            }

        } catch (Exception ignored) {
        }

        prefs.edit()
                .putString(
                        "trades",
                        array.toString()
                )
                .apply();
    }

    // -----------------------------
    // LOAD TRADES
    // -----------------------------

    void loadTrades() {

        String saved =
                prefs.getString(
                        "trades",
                        "[]"
                );

        try {

            JSONArray array =
                    new JSONArray(saved);

            for (int i = 0;
                 i < array.length();
                 i++) {

                JSONObject object =
                        array.getJSONObject(i);

                Trade trade =
                        new Trade(
                                object.optString("pair"),
                                object.optString("side"),
                                object.optString("result"),
                                object.optDouble("r"),
                                object.optString("setup"),
                                object.optString("session"),
                                object.optString("notes")
                        );

                trades.add(trade);
            }

        } catch (Exception ignored) {
        }
    }

    // -----------------------------
    // TRADE MODEL
    // -----------------------------

    static class Trade {

        String pair;
        String side;
        String result;
        double r;
        String setup;
        String session;
        String notes;

        Trade(
                String pair,
                String side,
                String result,
                double r,
                String setup,
                String session,
                String notes
        ) {

            this.pair = pair;
            this.side = side;
            this.result = result;
            this.r = r;
            this.setup = setup;
            this.session = session;
            this.notes = notes;
        }

        String summary() {

            return pair +
                    " • " +
                    side +
                    " • " +
                    result +
                    " • " +
                    String.format(
                            Locale.US,
                            "%.2fR",
                            r
                    );
        }
    }
    }
