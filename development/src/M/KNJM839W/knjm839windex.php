<?php

require_once('for_php7.php');

require_once('knjm839wModel.inc');
require_once('knjm839wQuery.inc');

class knjm839wController extends Controller {
    var $ModelClassName = "knjm839wModel";
    var $ProgramID      = "KNJM839W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm839wForm1");
                    }
                    break 2;
                case "":
                case "knjm839w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm839wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm839wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm839wCtl = new knjm839wController;
//var_dump($_REQUEST);
?>

