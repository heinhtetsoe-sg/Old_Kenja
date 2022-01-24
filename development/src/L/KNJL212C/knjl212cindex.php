<?php

require_once('for_php7.php');

require_once('knjl212cModel.inc');
require_once('knjl212cQuery.inc');

class knjl212cController extends Controller {
    var $ModelClassName = "knjl212cModel";
    var $ProgramID      = "KNJL212C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl212c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl212cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl212cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl212cCtl = new knjl212cController;
var_dump($_REQUEST);
?>
