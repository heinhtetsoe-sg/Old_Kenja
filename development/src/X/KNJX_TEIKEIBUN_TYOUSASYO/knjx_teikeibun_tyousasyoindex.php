<?php

require_once('for_php7.php');

require_once('knjx_teikeibun_tyousasyoModel.inc');
require_once('knjx_teikeibun_tyousasyoQuery.inc');

class knjx_teikeibun_tyousasyoController extends Controller {
    var $ModelClassName = "knjx_teikeibun_tyousasyoModel";
    var $ProgramID      = "KNJX_TEIKEIBUN_TYOUSASYO";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "teikei_act":
                case "teikei_val":
                    $this->callView("knjx_teikeibun_tyousasyoForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_teikeibun_tyousasyoCtl = new knjx_teikeibun_tyousasyoController;
?>
