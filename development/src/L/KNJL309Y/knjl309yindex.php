<?php

require_once('for_php7.php');

require_once('knjl309yModel.inc');
require_once('knjl309yQuery.inc');

class knjl309yController extends Controller {
    var $ModelClassName = "knjl309yModel";
    var $ProgramID      = "KNJL309Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl309y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl309yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl309yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl309yCtl = new knjl309yController;
?>
