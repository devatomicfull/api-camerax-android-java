package usando.camerax.core;

import android.content.Context;

import androidx.camera.core.CameraSelector;
import androidx.camera.view.LifecycleCameraController;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe auxiliar (helper) para configuração da câmera utilizando a API CameraX.
 *
 * Responsabilidades:
 * - Inicializa e configura o LifecycleCameraController com a câmera traseira padrão.
 * - Ativa o caso de uso de captura de imagem.
 * - Vincula a câmera ao ciclo de vida da Activity ou Fragment fornecido.
 * - Fornece um ExecutorService para processar operações da câmera em thread separada.
 * - Encapsula toda a lógica de configuração da câmera para reutilização e clareza do código.
 *
 * Exemplo de uso:
 *
 * CameraHelper cameraHelper = new CameraHelper(requireContext(), getViewLifecycleOwner());
 * PreviewView previewView = findViewById(R.id.previewView);
 * previewView.setController(cameraHelper.getControlador());
 *
 * // Ao encerrar a Activity/Fragment:
 * cameraHelper.encerrar();
 */
public class CameraHelper {

    // Contexto da aplicação ou activity necessário para criar o controlador da câmera
    private final Context contexto;

    // Ciclo de vida da Activity ou Fragment usado para bind da câmera
    private final LifecycleOwner cicloDeVida;

    // Controlador que gerencia a câmera com base no ciclo de vida
    private final LifecycleCameraController controlador;

    // Executor de thread único para processar operações de câmera em background
    private final ExecutorService executor;

    /**
     * Construtor da classe CameraHelper.
     *
     * @param contexto     Contexto da aplicação ou componente (Activity/Fragment)
     * @param cicloDeVida  Objeto responsável por fornecer o ciclo de vida atual (ex: getViewLifecycleOwner)
     */
    public CameraHelper(Context contexto, LifecycleOwner cicloDeVida) {
        this.contexto = contexto;
        this.cicloDeVida = cicloDeVida;

        // Cria um controlador de câmera associado ao contexto
        this.controlador = new LifecycleCameraController(contexto);

        // Cria um executor com uma única thread para operações da câmera
        this.executor = Executors.newSingleThreadExecutor();

        // Configura a câmera com os parâmetros desejados
        configurar();
    }

    /**
     * Método responsável por configurar o controlador da câmera.
     * Define a câmera traseira como padrão, ativa a captura de imagem e vincula ao ciclo de vida.
     */
    private void configurar() {
        // Define o uso da câmera traseira (DEFAULT_BACK_CAMERA)
        controlador.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);

        // Habilita o uso de captura de imagem (IMAGE_CAPTURE)
        controlador.setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE);

        // Vincula o controlador da câmera ao ciclo de vida do componente
        controlador.bindToLifecycle(cicloDeVida);
    }

    /**
     * Retorna o controlador da câmera configurado.
     * Pode ser usado para associar ao PreviewView ou iniciar capturas.
     *
     * @return LifecycleCameraController configurado.
     */
    public LifecycleCameraController getControlador() {
        return controlador;
    }

    /**
     * Retorna o executor usado para tarefas assíncronas da câmera.
     * Usado, por exemplo, ao chamar takePicture().
     *
     * @return ExecutorService com uma thread única.
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Método para liberar os recursos do ExecutorService.
     * Deve ser chamado, por exemplo, no onDestroyView() ou onDestroy().
     */
    public void encerrar() {
        if (executor != null) executor.shutdown();
    }
}
