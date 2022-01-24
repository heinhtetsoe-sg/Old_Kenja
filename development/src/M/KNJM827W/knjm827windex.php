<?php
require_once('knjm827wModel.inc');
require_once('knjm827wQuery.inc');

class knjm827wController extends Controller {
    var $ModelClassName = "knjm827wModel";
    var $ProgramID      = "KNJM827W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm827w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm827wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm827wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm827wCtl = new knjm827wController;
//var_dump($_REQUEST);
?>

