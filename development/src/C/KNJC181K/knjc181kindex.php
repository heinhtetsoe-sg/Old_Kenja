<?php

require_once('for_php7.php');

require_once('knjc181kModel.inc');
require_once('knjc181kQuery.inc');

class knjc181kController extends Controller {
    var $ModelClassName = "knjc181kModel";
    var $ProgramID      = "KNJC181K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc181k":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc181kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjc181kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc181kcCtl = new knjc181kController;
//var_dump($_REQUEST);
?>
