<?php

require_once('for_php7.php');

require_once('knjl329rModel.inc');
require_once('knjl329rQuery.inc');

class knjl329rController extends Controller {
    var $ModelClassName = "knjl329rModel";
    var $ProgramID      = "KNJL329R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl329r":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl329rModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl329rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl329rCtl = new knjl329rController;
//var_dump($_REQUEST);
?>
