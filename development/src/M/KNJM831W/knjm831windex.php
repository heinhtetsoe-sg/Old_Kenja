<?php
require_once('knjm831wModel.inc');
require_once('knjm831wQuery.inc');

class knjm831wController extends Controller {
    var $ModelClassName = "knjm831wModel";
    var $ProgramID      = "KNJM831W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm831w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm831wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm831wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm831wCtl = new knjm831wController;
//var_dump($_REQUEST);
?>

