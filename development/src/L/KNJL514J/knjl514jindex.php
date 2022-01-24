<?php

require_once('for_php7.php');

require_once('knjl514jModel.inc');
require_once('knjl514jQuery.inc');

class knjl514jController extends Controller {
    var $ModelClassName = "knjl514jModel";
    var $ProgramID      = "KNJL514J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                //データ生成(mirai → 賢者)
                //CSV取込
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                //CSV出力
                case "csv":
                case "head":
                case "error":
                case "data":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl514jForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl514jForm1");
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
$knjl514jCtl = new knjl514jController;
?>
