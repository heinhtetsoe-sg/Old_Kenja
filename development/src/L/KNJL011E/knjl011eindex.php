<?php

require_once('for_php7.php');

require_once('knjl011eModel.inc');
require_once('knjl011eQuery.inc');

class knjl011eController extends Controller {
    var $ModelClassName = "knjl011eModel";
    var $ProgramID      = "KNJL011E";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeTest":
                case "back1":
                case "next1":
                    $this->callView("knjl011eForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011eForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011eForm1");
                    break 2;
                case "exec_pdf":  //PDFアップロード
                    $sessionInstance->getExecPdfModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "pdf_del":   //PDF削除
                    $sessionInstance->getPdfDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl011eForm1");
                    break 2;
                case "reset":
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl011eCtl = new knjl011eController;
?>
