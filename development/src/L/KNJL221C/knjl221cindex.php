<?php

require_once('for_php7.php');

require_once('knjl221cModel.inc');
require_once('knjl221cQuery.inc');

class knjl221cController extends Controller {
    var $ModelClassName = "knjl221cModel";
    var $ProgramID      = "KNJL221C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl221c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl221cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl221cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl221cCtl = new knjl221cController;
var_dump($_REQUEST);
?>
