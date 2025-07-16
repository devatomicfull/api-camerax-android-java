package teste1.cameraxsample1.camera;

import android.content.ContentValues;
import android.content.Context;

import android.provider.MediaStore;

import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;

import androidx.camera.lifecycle.ProcessCameraProvider;

import androidx.camera.view.PreviewView;

import androidx.core.content.ContextCompat;

import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class ControladorCamera {

    private static final String TAG = "ControladorCamera";
    private final Context contexto;
    private final LifecycleOwner donoCicloVida;
    private final PreviewView visualizacaoCamera;
    private ImageCapture capturaImagem;
    private final Executor executor;

    public ControladorCamera(Context contexto, LifecycleOwner donoCicloVida, PreviewView visualizacaoCamera) {
        this.contexto = contexto;
        this.donoCicloVida = donoCicloVida;
        this.visualizacaoCamera = visualizacaoCamera;
        this.executor = ContextCompat.getMainExecutor(contexto);
    }

    public void iniciarCamera() {
        ListenableFuture<ProcessCameraProvider> futuroProvedorCamera = ProcessCameraProvider.getInstance(contexto);
        futuroProvedorCamera.addListener(() -> configurarCasosDeUsoCamera(obterProvedorCamera(futuroProvedorCamera)), executor);
    }

    private ProcessCameraProvider obterProvedorCamera(ListenableFuture<ProcessCameraProvider> futuro) {
        try {
            return futuro.get();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter provedor de câmera", e);
            throw new RuntimeException("Falha ao inicializar câmera", e);
        }
    }

    private void configurarCasosDeUsoCamera(ProcessCameraProvider provedorCamera) {
        Preview visualizacao = configurarVisualizacao();
        capturaImagem = configurarCapturaImagem();
        CameraSelector seletorCamera = CameraSelector.DEFAULT_BACK_CAMERA;

        provedorCamera.unbindAll();
        provedorCamera.bindToLifecycle(donoCicloVida, seletorCamera, visualizacao, capturaImagem);
    }

    private Preview configurarVisualizacao() {
        Preview visualizacao = new Preview.Builder()
                .setTargetResolution(new Size(visualizacaoCamera.getWidth(), visualizacaoCamera.getHeight()))
                .build();
        visualizacao.setSurfaceProvider(visualizacaoCamera.getSurfaceProvider());
        return visualizacao;
    }

    private ImageCapture configurarCapturaImagem() {
        return new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
    }

    public void capturarFoto(@NonNull ImageCapture.OnImageSavedCallback callback) {
        if (capturaImagem == null) {
            Log.w(TAG, "Caso de uso de captura de imagem não inicializado");
            return;
        }

        ImageCapture.OutputFileOptions opcoesSaida = criarOpcoesSaidaArquivo();
        capturaImagem.takePicture(opcoesSaida, executor, callback);
    }

    private ImageCapture.OutputFileOptions criarOpcoesSaidaArquivo() {
        String nomeArquivo = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";
        ContentValues valoresConteudo = new ContentValues();
        valoresConteudo.put(MediaStore.MediaColumns.DISPLAY_NAME, nomeArquivo);
        valoresConteudo.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        valoresConteudo.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/CameraXApp");

        return new ImageCapture.OutputFileOptions.Builder(
                contexto.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                valoresConteudo
        ).build();
    }
}