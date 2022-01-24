<?php

require_once('for_php7.php');

require_once('knjl322kModel.inc');
require_once('knjl322kQuery.inc');

class knjl322kController extends Controller {
    var $ModelClassName = "knjl322kModel";
    var $ProgramID      = "KNJL322K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322k":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl322kModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl322kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl322kCtl = new knjl322kController;
var_dump($_REQUEST);
?>
