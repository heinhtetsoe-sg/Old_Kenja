<?php

require_once('for_php7.php');

require_once('knjl332qModel.inc');
require_once('knjl332qQuery.inc');

class knjl332qController extends Controller {
    var $ModelClassName = "knjl332qModel";
    var $ProgramID      = "KNJL332Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl332q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl332qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl332qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl332qCtl = new knjl332qController;
?>
