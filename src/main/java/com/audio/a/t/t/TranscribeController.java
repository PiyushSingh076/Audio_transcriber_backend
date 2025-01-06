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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/transcribe")
public class TranscribeController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    //custom definiton of the constructor for this usage (have to remember)
    public TranscribeController(@Value("${spring.ai.openai.api-key}") String apikey) {
        OpenAiAudioApi openAiApi = new OpenAiAudioApi(apikey);
        this.transcriptionModel = new OpenAiAudioTranscriptionModel(openAiApi); //u need openaiapi object to create object of this
    }

    @PostMapping
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        File tempfile = File.createTempFile("audio",".wav");
        file.transferTo(tempfile);
        //this is the file that we upload we set it into the template

        //...options is basically instructions as of how to proceed with that file and its transcription (this is what open ai understands)
        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withLanguage("en")
                .withTemperature(0f)
                .build();

        FileSystemResource audioFile = new FileSystemResource(tempfile); //wraps the file for tanscription api to make use

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile,transcriptionOptions); //send file and options to model
        AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);//model is encapsulated with the method to generate the transcription

        tempfile.delete();
        return new ResponseEntity<>(response.getResult().getOutput(), HttpStatus.OK);
    }
}
