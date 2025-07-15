package usando.camerax;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import usando.camerax.core.CameraHelper;

public class MainActivity extends AppCompatActivity {

    private CameraHelper cameraHelper;
    private PreviewView previewView;

    private final ActivityResultLauncher<String> permissaoCameraLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    iniciarCamera();
                } else {
                    Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Chama a implementação da superclasse para restaurar o estado padrão da Activity
        super.onCreate(savedInstanceState);

        // Define o layout XML a ser utilizado como interface da Activity
        setContentView(R.layout.activity_main);

        // Referência ao componente PreviewView onde será exibida a imagem da câmera
        previewView = findViewById(R.id.cameraPreview);

        // Obtém referência ao botão de captura e define o evento de clique
        Button botaoFoto = findViewById(R.id.btnCapturar);

        // Quando o botão for clicado, chama o método para capturar a foto
        botaoFoto.setOnClickListener(view -> capturarFoto());

        /**
         * Verifica se a permissão de uso da câmera já foi concedida.
         * - Se sim, chama o método iniciarCamera() para configurar e iniciar a câmera.
         * - Se não, solicita a permissão usando um launcher (provavelmente criado com registerForActivityResult).
         *
         * Obs: A permissão é necessária em tempo de execução a partir do Android 6.0 (API 23).
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            iniciarCamera();
        } else {
            // Solicita a permissão ao usuário
            permissaoCameraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Método responsável por inicializar a câmera usando a classe CameraHelper.
     *
     * Responsabilidade:
     * - Cria uma instância de CameraHelper, passando o contexto e o ciclo de vida da Activity.
     * - Obtém o controlador da câmera do helper e associa-o ao PreviewView para exibir a imagem da câmera.
     *
     * Esse método só deve ser chamado após a permissão da câmera ter sido concedida.
     */
    private void iniciarCamera() {
        // Cria e configura a câmera usando a classe helper
        cameraHelper = new CameraHelper(this, this);

        // Associa o controlador da câmera ao PreviewView, permitindo visualização em tempo real
        previewView.setController(cameraHelper.getControlador());
    }

    /**
     * Método responsável por capturar uma foto utilizando o controlador de câmera do CameraHelper.
     *
     * Funcionamento:
     * - Verifica se o objeto cameraHelper está inicializado.
     * - Gera um nome de arquivo único com base na data e hora atuais.
     * - Cria um arquivo local no diretório de arquivos externos do app.
     * - Configura as opções de saída da captura (OutputFileOptions).
     * - Dispara a captura de imagem com callback para sucesso e erro.
     *
     * O callback:
     * - Em caso de sucesso, exibe um Toast com o caminho onde a foto foi salva.
     * - Em caso de erro, exibe um Toast informando a falha.
     *
     * Observações:
     * - O método utiliza runOnUiThread() para garantir que as notificações (Toast)
     *   sejam feitas na thread principal (UI), pois os callbacks são executados em background.
     */
    private void capturarFoto() {
        // Garante que o CameraHelper foi inicializado antes de prosseguir
        if (cameraHelper == null) return;

        // Cria um nome único para o arquivo com base na data/hora atual no formato: yyyyMMdd_HHmmss.jpg
        String nome = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg";

        // Define o caminho do arquivo onde a imagem será salva (pasta privada do app)
        File arquivo = new File(getExternalFilesDir(null), nome);

        // Define as opções de saída, informando o arquivo onde a imagem será salva
        ImageCapture.OutputFileOptions opcoes =
                new ImageCapture.OutputFileOptions.Builder(arquivo).build();

        // Solicita a captura da imagem com o controlador do CameraHelper
        cameraHelper.getControlador().takePicture(
                opcoes,                          // Opções de saída (arquivo de destino)
                cameraHelper.getExecutor(),      // Executor que processará a captura (thread em background)
                new ImageCapture.OnImageSavedCallback() {

                    // Chamado quando a imagem for salva com sucesso
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> Toast.makeText(
                                MainActivity.this,
                                "Foto salva: " + arquivo.getAbsolutePath() + " ",
                                Toast.LENGTH_LONG
                        ).show());
                    }

                    // Chamado em caso de erro ao salvar a imagem
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() -> Toast.makeText(
                                MainActivity.this,
                                "Erro ao salvar foto",
                                Toast.LENGTH_SHORT
                        ).show());
                    }
                });
    }

    /**
     * Método de ciclo de vida da Activity chamado quando ela está prestes a ser destruída.
     *
     * Responsabilidade:
     * - Garantir a liberação correta de recursos utilizados pela câmera, especialmente o ExecutorService,
     *   evitando vazamentos de memória (memory leaks) ou threads em execução após o encerramento da Activity.
     *
     * Uso:
     * - Esse método sobrescreve o onDestroy da Activity padrão do Android.
     * - Verifica se o objeto cameraHelper foi instanciado.
     * - Chama cameraHelper.encerrar() para encerrar o ExecutorService.
     */
    @Override
    protected void onDestroy() {
        // Chamada à implementação original do Android para garantir o fluxo normal de destruição
        super.onDestroy();

        // Verifica se o helper da câmera foi criado
        if (cameraHelper != null) {
            // Libera o ExecutorService associado à câmera
            cameraHelper.encerrar();
        }
    }
}
