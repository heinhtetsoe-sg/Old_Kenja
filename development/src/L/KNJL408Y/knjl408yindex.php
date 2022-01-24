<?php

require_once('for_php7.php');

require_once('knjl408yModel.inc');
require_once('knjl408yQuery.inc');

class knjl408yController extends Controller {
    var $ModelClassName = "knjl408yModel";
    var $ProgramID      = "KNJL408Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl408y":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl408yModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl408yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl408yCtl = new knjl408yController;
?>
