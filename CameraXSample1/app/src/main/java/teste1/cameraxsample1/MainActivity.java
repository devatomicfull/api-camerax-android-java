package teste1.cameraxsample1;

import android.Manifest;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;

import teste1.cameraxsample1.camera.ControladorCamera;
import teste1.cameraxsample1.permissions.GerenciadorPermissoes;

public class MainActivity extends AppCompatActivity {

    private static final int CODIGO_REQUISICAO_PERMISSOES = 10;
    private static final String[] PERMISSOES_NECESSARIAS = {Manifest.permission.CAMERA};

    private ControladorCamera controladorCamera;
    private GerenciadorPermissoes gerenciadorPermissoes;
    private ImageButton botaoCapturarFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarComponentes();
        configurarAcaoBotaoCaptura();
    }

    private void inicializarComponentes() {
        gerenciadorPermissoes = new GerenciadorPermissoes(this);
        botaoCapturarFoto = findViewById(R.id.botao_capturar_foto);
        controladorCamera = new ControladorCamera(this, this, findViewById(R.id.visualizacao_camera));

        if (gerenciadorPermissoes.possuiPermissoes(PERMISSOES_NECESSARIAS)) {
            controladorCamera.iniciarCamera();
        } else {
            gerenciadorPermissoes.solicitarPermissoes(this, PERMISSOES_NECESSARIAS, CODIGO_REQUISICAO_PERMISSOES);
        }
    }

    private void configurarAcaoBotaoCaptura() {
        botaoCapturarFoto.setOnClickListener(v -> controladorCamera.capturarFoto
                (new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Foto salva com sucesso!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Erro ao salvar foto: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }));
    }

    @Override
    public void onRequestPermissionsResult(int codigoRequisicao, @NonNull String[] permissoes, @NonNull int[] resultados) {
        super.onRequestPermissionsResult(codigoRequisicao, permissoes, resultados);
        if (codigoRequisicao == CODIGO_REQUISICAO_PERMISSOES) {
            if (gerenciadorPermissoes.verificarResultadosPermissoes(resultados)) {
                controladorCamera.iniciarCamera();
            } else {
                Toast.makeText(this, "Permissões negadas. O aplicativo será encerrado.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}