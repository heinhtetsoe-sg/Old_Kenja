<?php

require_once('for_php7.php');

require_once('knjl344fModel.inc');
require_once('knjl344fQuery.inc');

class knjl344fController extends Controller {
    var $ModelClassName = "knjl344fModel";
    var $ProgramID      = "KNJL344F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl344f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl344fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl344fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl344fCtl = new knjl344fController;
?>
