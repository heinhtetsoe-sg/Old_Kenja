<?php

require_once('for_php7.php');

require_once('knjl400yModel.inc');
require_once('knjl400yQuery.inc');

class knjl400yController extends Controller {
    var $ModelClassName = "knjl400yModel";
    var $ProgramID      = "KNJL400Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl400y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl400yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl400yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjl400yCtl = new knjl400yController;
?>
