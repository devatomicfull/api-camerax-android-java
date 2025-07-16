package teste1.cameraxsample1.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GerenciadorPermissoes {

    private final Context contexto;

    public GerenciadorPermissoes(Context contexto) {
        this.contexto = contexto;
    }

    public boolean possuiPermissoes(String[] permissoes) {
        for (String permissao : permissoes) {
            if (ContextCompat.checkSelfPermission(contexto, permissao) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void solicitarPermissoes(Activity atividade, String[] permissoes, int codigoRequisicao) {
        ActivityCompat.requestPermissions(atividade, permissoes, codigoRequisicao);
    }

    public boolean verificarResultadosPermissoes(int[] resultados) {
        for (int resultado : resultados) {
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}