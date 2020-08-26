package com.example.hellonotes.ui.acitivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.hellonotes.ui.utils.AppConstant;
import com.example.hellonotes.ui.model.Note;
import com.example.hellonotes.ui.viewmodel.NoteViewModel;
import com.example.hellonotes.R;
import com.example.hellonotes.ui.adapter.NoteAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomSheetBehavior behavior;
    private FloatingActionButton btnFab;
    private AppCompatButton btnAddNote;
    private EditText edtTitle, edtDescription;
    private NoteViewModel noteViewModel;
    private ImageView imgClose;
    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;
    private SwitchCompat swtIsFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        btnFab = findViewById(R.id.button_fab);
        btnAddNote = findViewById(R.id.btnAddNote);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        imgClose = findViewById(R.id.imgClose);
        swtIsFavorite = findViewById(R.id.swtIsFavorite);

        initUI();
    }


    private void initUI() {

        RecyclerView recyclerView = findViewById(R.id.recyclerView_note);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        final NoteAdapter adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                adapter.setNotes(notes);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                edtTitle.setText(note.getTitle());
                edtDescription.setText(note.getDescription());
                AppConstant.currentID = note.getId();
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                btnAddNote.setText(R.string.update);
                swtIsFavorite.setChecked(note.isFavorite());
                AppConstant.isFromFab = "item";
            }
        });


        // fab button on clink
        btnFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddNoteScreen();
            }
        });


        // add note button
        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        // bottom sheet close
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


    }

    private void openAddNoteScreen() {
        if (behavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        AppConstant.isFromFab = "fav";
        edtTitle.setText("");
        edtDescription.setText("");
        btnAddNote.setText(R.string.add_note);
    }

    private void saveNote() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();
        boolean isFavoriteChecked;
        if (swtIsFavorite.isChecked()) {
            isFavoriteChecked = true;
        } else {
            isFavoriteChecked = false;
        }

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, R.string.error_title, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(desc)) {
            Toast.makeText(this, R.string.error_desc, Toast.LENGTH_SHORT).show();
        } else {

            if (AppConstant.isFromFab.equalsIgnoreCase("fav")) {
                // add note
                Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show();
                Note note = new Note(title, desc, isFavoriteChecked);
                noteViewModel.insert(note);
                edtTitle.setText("");
                edtDescription.setText("");
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            } else {
                // update note
                Note note = new Note(title, desc, isFavoriteChecked);
                note.setId(AppConstant.currentID);
                noteViewModel.update(note);
                edtTitle.setText("");
                edtDescription.setText("");
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (behavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), R.string.back_warning, Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        } else {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }
}