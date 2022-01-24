<?php

require_once('for_php7.php');

require_once('knjl254cModel.inc');
require_once('knjl254cQuery.inc');

class knjl254cController extends Controller {
    var $ModelClassName = "knjl254cModel";
    var $ProgramID      = "KNJL254C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl254c":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl254cModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl254cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl254cCtl = new knjl254cController;
?>
