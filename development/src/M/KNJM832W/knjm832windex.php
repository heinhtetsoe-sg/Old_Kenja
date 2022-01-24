<?php
require_once('knjm832wModel.inc');
require_once('knjm832wQuery.inc');

class knjm832wController extends Controller {
    var $ModelClassName = "knjm832wModel";
    var $ProgramID      = "KNJM832W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm832w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm832wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm832wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm832wCtl = new knjm832wController;
//var_dump($_REQUEST);
?>

