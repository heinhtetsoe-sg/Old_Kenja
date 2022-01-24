<?php

require_once('for_php7.php');

require_once('knjl305qModel.inc');
require_once('knjl305qQuery.inc');

class knjl305qController extends Controller {
    var $ModelClassName = "knjl305qModel";
    var $ProgramID      = "KNJL305Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl305qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl305qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl305qCtl = new knjl305qController;
?>
