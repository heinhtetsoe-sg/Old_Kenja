<?php

require_once('for_php7.php');

require_once('knjl344qModel.inc');
require_once('knjl344qQuery.inc');

class knjl344qController extends Controller {
    var $ModelClassName = "knjl344qModel";
    var $ProgramID      = "KNJL344Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl344q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl344qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl344qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl344qCtl = new knjl344qController;
?>
