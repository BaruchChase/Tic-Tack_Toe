package com.example.tic_tack_toe.activities;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import static com.example.tic_tack_toe.lib.Utils.showInfoDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.tic_tack_toe.R;
import com.example.tic_tack_toe.databinding.ActivityMainBinding;
import com.example.tic_tack_toe.databinding.ContentMainBinding;
import com.google.gson.Gson;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
// todo create settings page

public class MainActivity extends AppCompatActivity {

    // fields
    Symbol[][] mGameBord = new Symbol[3][3];
    Button[][] mButtonBord;

    enum Turn {PLAYER1, PLAYER2}

    enum GameStatus {CONTINUE, OVER}

    enum Symbol {X, O, EMPTY}

    Turn curPlayer;
    Turn winner;
    GameStatus curGameStatus;
    TextView tv;
    int cellCounter;
    private ActivityMainBinding binding;

    private final String mKEY_GAME = "GAME";
    private final String mKeyFilledGameBord = "FILLED_GAMEBORD";
    private final String mKeyPlayerTurn = "PLAYER_TURN";
    private final String mKeyCurGameStatus = "GAME_STATUS";
    private final String mKeyCellCounter = "CELL_COUNTER";
    private boolean mUseAutoSave;
    private String mKEY_AUTO_SAVE;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> mGBordAsList = toList(mGameBord);
        outState.putStringArrayList(mKeyFilledGameBord, mGBordAsList);
        outState.putString(mKeyPlayerTurn, curPlayer.toString());
        outState.putInt(mKeyCellCounter, cellCounter);
        outState.putString(mKeyCurGameStatus, curGameStatus.toString());
    }

    private Symbol[][] fromList(ArrayList<String> list) {
        Symbol[][] recoveredBord = new Symbol[3][3];
        Symbol s;
        Iterator<String> listIterator = list.iterator();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                s = getSymbol(listIterator.next());
                recoveredBord[row][col] = s;
            }
        }
        return recoveredBord;
    }

    private Symbol getSymbol(Object next) {
        if ("X".equals(next)) {
            return Symbol.X;
        } else if ("O".equals(next)) {
            return Symbol.O;
        } else if ("EMPTY".equals(next)) {
            return Symbol.EMPTY;
        } else {
            return null;
        }
    }

    private ArrayList<String> toList(Symbol[][] mGameBord) {
        ArrayList<String> list = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                list.add(mGameBord[row][col] + "");
            }
        }
        return list;
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        saveOrDeleteGameInSharedPreferences();
//    }
//
//    private void saveOrDeleteGameInSharedPreferences() {
//        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
//
//        if (mUseAutoSave) {
//            editor.putString("Game", this.getJSONFromCurrentGame());
//        }
//        else {
//            editor.remove("Game");
//        }
//        editor.apply();
//    }
//    public String getJSONFromCurrentGame()
//    {
//        return getJSONFromGame(this);
//    }
//
//    private String getJSONFromGame(MainActivity mainActivity) {
//        Gson gson = new Gson ();
//        return gson.toJson (mainActivity);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        restorePreferences_SavedGameIfAutoSaveWasSetOn();
//        restoreOrSetFromPreferences_AllAppAndGameSettings();
//    }
//    private void restorePreferences_SavedGameIfAutoSaveWasSetOn() {
//        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
//        if (defaultSharedPreferences.getBoolean(mKEY_AUTO_SAVE, true)) {
//            String gameString = defaultSharedPreferences.getString(mKEY_GAME, null);
//            if (gameString != null) {
//                binding = this.getGameFromJSON(gameString);
//                updateUI();
//            }
//        }
//    }
//
//    private ActivityMainBinding getGameFromJSON(String json) {
//        Gson gson = new Gson ();
//        return gson.fromJson (json, (Type) MainActivity.class);
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mKEY_AUTO_SAVE = getString(R.string.auto_save_key);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ContentMainBinding cm = binding.constrainCard.contentMain;

        mButtonBord = new Button[][]{{cm.topLeft, cm.topMiddle, cm.topRight},
                {cm.middleLeft, cm.middleMiddle, cm.middleRight},
                {cm.bottomLeft, cm.bottomMiddle, cm.bottomRight}};

        initializeGameBord(savedInstanceState);

    }

    private void initializeGameBord(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            restoreGameFromBundle(savedInstanceState);
        } else {
            createGameBord();
            curPlayer = Turn.PLAYER1;
            curGameStatus = GameStatus.CONTINUE;
            tv = findViewById(R.id.cur_player);
            tv.setText(R.string.game_startMessage);
            cellCounter = 0;
        }
    }

    private void restoreGameFromBundle(Bundle savedInstanceState) {
        mGameBord = fromList(savedInstanceState.getStringArrayList(mKeyFilledGameBord));
        curPlayer = savedInstanceState.getString(mKeyPlayerTurn).equals("PLAYER1") ? Turn.PLAYER1
                : Turn.PLAYER2;
        curGameStatus = savedInstanceState.getString(mKeyCurGameStatus).equals("OVER") ? GameStatus.OVER
                : GameStatus.CONTINUE;
        tv = findViewById(R.id.cur_player);
        if (curGameStatus == GameStatus.OVER) {
            tv.setText(R.string.game_over);
        }
        else {
        tv.setText(formatTurnMessage(curPlayer));
        }
        cellCounter = savedInstanceState.getInt(mKeyCellCounter);

        updateUI();
    }

    private void updateUI() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (mGameBord[row][col] != Symbol.EMPTY) {
                    Button curButton = mButtonBord[row][col];
                    curButton.setText(mGameBord[row][col].toString());
                }
            }
        }
    }

    private void createGameBord() {
        mGameBord = new Symbol[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mGameBord[i][j] = Symbol.EMPTY;
            }
        }
    }

    public void processTurn(View view) throws Exception {
        if (curGameStatus.equals(GameStatus.CONTINUE)) {

            androidx.appcompat.widget.AppCompatButton curButton =
                    (androidx.appcompat.widget.AppCompatButton) view;
            String tag = (String) curButton.getTag();
            int row = tag.charAt(0) - 48;
            int col = tag.charAt(2) - 48;

            Symbol cell = mGameBord[row][col];

            if (cell.equals(Symbol.EMPTY)) {

                Symbol curSym = curPlayer.equals(Turn.PLAYER1) ? Symbol.X : Symbol.O;
                setButtonText(curSym, curButton);
                mGameBord[row][col] = curSym;
                System.out.println(row + " : " + col + " : " + mGameBord[row][col]);

                if (isWinCondition(curSym)) {
                    curGameStatus = GameStatus.OVER;
                    winner = curPlayer;
                    String message = formatWinMessage(curPlayer);
                    tv.setText(message);
                } else if (cellCounter >= 9) {
                    curGameStatus = GameStatus.OVER;
                } else {
                    cellCounter++;
                    curPlayer = curPlayer.equals(Turn.PLAYER1) ? Turn.PLAYER2 : Turn.PLAYER1;
                    String message = formatTurnMessage(curPlayer);
                    tv.setText(message);

                }

            } else {
                System.out.println("full");
                tv.setText(R.string.full_space);
            }
        } else {
            tv.setText(R.string.game_over);
        }
    }

    @NonNull
    private String formatWinMessage(Turn curPlayer) {
        String message = curPlayer.equals(Turn.PLAYER1) ? "Player 1" : "Player 2";
        return message + " Wins!";
    }

    private String formatTurnMessage(Turn curPlayer) {
        String message = curPlayer.equals(Turn.PLAYER1) ? "Player 1" : "Player 2";
        return message + "'s Turn";
    }

    private boolean isWinCondition(Symbol curSym) {

        return isRowWin(curSym) || isColWin(curSym) || isDiagWin(curSym);
    }

    private boolean isDiagWin(Symbol curSym) {
        if (topLeftWinDiag(curSym)) {
            return true;
        } else if (topRightWinDiag(curSym)) {
            return true;
        }
        return false;
    }

    private boolean topRightWinDiag(Symbol curSym) {
        int row = 0;
        int col = 2;
        for (int i = 0; i < 3; i++) {
            if (!(mGameBord[col][row].equals(curSym))) {
                return false;
            } else {
                row++;
                col--;
            }
        }
        return true;
    }

    private boolean topLeftWinDiag(Symbol curSym) {
        for (int i = 0; i < 3; i++) {
            if (!(mGameBord[i][i].equals(curSym))) {
                return false;
            }
        }
        return true;
    }

    private boolean isColWin(Symbol curSym) {
        ;
        if (downCol(curSym, 0)) {
            return true;
        } else if (downCol(curSym, 1)) {
            return true;
        } else if (downCol(curSym, 2)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean downCol(Symbol curSym, int col) {
        for (int row = 0; row < 3; row++) {
            if (!(mGameBord[col][row].equals(curSym))) {
                return false;
            }
        }
        return true;
    }

    private boolean isRowWin(Symbol curSym) {
        if (acrossRow(curSym, 0)) {
            return true;
        } else if (acrossRow(curSym, 1)) {
            return true;
        } else if (acrossRow(curSym, 2)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean acrossRow(Symbol curSym, int row) {
        for (int col = 0; col < 3; col++) {
            if (!(mGameBord[col][row].equals(curSym))) {
                return false;
            }
        }
        return true;
    }

    private void setButtonText(Symbol curSym, Button curButton) throws Exception {
        switch (curSym) {
            case X:
                curButton.setText("X");
                break;
            case O:
                curButton.setText("O");
                break;
            default:
                throw new Exception("Help! Exceptions! this input was invalid!");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_game) {
            startNewGame();
            return true;
        }
        else if (id == R.id.settings_page) {
            showSettings();
            return true;
        }
        else if (id == R.id.about_page) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout() {
        showInfoDialog(MainActivity.this, "About Tick Tack Toe",
                "A quick two-player game; X goes first.");
    }

    private void showSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        settingsLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> restoreOrSetFromPreferences_AllAppAndGameSettings());

    private void restoreOrSetFromPreferences_AllAppAndGameSettings() {
        SharedPreferences sp = getDefaultSharedPreferences(this);
        mUseAutoSave = sp.getBoolean(mKEY_AUTO_SAVE, true);
    }

    private void startNewGame() {
        createGameBord();
        clearBord();
        curPlayer = Turn.PLAYER1;
        curGameStatus = GameStatus.CONTINUE;
        tv.setText("Player 1's Turn");
        cellCounter = 0;
    }

    private void clearBord() {
        Button curButton;
        // top row
        curButton = findViewById(R.id.top_left);
        curButton.setText("");
        curButton = findViewById(R.id.top_middle);
        curButton.setText("");
        curButton = findViewById(R.id.top_right);
        curButton.setText("");

        // middle row
        curButton = findViewById(R.id.middle_left);
        curButton.setText("");
        curButton = findViewById(R.id.middle_middle);
        curButton.setText("");
        curButton = findViewById(R.id.middle_right);
        curButton.setText("");

        // bottom row
        curButton = findViewById(R.id.bottom_left);
        curButton.setText("");
        curButton = findViewById(R.id.bottom_middle);
        curButton.setText("");
        curButton = findViewById(R.id.bottom_right);
        curButton.setText("");
    }


}