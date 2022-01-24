<?php

require_once('for_php7.php');

require_once('knjl253cModel.inc');
require_once('knjl253cQuery.inc');

class knjl253cController extends Controller {
    var $ModelClassName = "knjl253cModel";
    var $ProgramID      = "KNJL253C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl253c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl253cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl253cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl253cCtl = new knjl253cController;
var_dump($_REQUEST);
?>
