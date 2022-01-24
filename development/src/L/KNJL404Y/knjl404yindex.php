<?php

require_once('for_php7.php');

require_once('knjl404yModel.inc');
require_once('knjl404yQuery.inc');

class knjl404yController extends Controller {
    var $ModelClassName = "knjl404yModel";
    var $ProgramID      = "KNJL404Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl404y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl404yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl404yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl404yCtl = new knjl404yController;
?>
