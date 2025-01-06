package com.audio.a.t.t;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/transcribe")
public class TranscribeController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public TranscribeController(@Value("${spring.ai.openai.api-key}") String apikey) {
        OpenAiAudioApi openAiApi = new OpenAiAudioApi(apikey);
        this.transcriptionModel = new OpenAiAudioTranscriptionModel(openAiApi);
    }

    @PostMapping
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        File tempfile = File.createTempFile("audio", ".wav");

        try (FileOutputStream fileOutputStream = new FileOutputStream(tempfile, true);
             InputStream inputStream = file.getInputStream()) {

            // Append the audio data into the temp file
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withLanguage("en")
                .withTemperature(0f)
                .build();

        FileSystemResource audioFile = new FileSystemResource(tempfile);

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);

        tempfile.delete();
        return new ResponseEntity<>(response.getResult().getOutput(), HttpStatus.OK);
    }
}
