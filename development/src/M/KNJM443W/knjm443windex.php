<?php

require_once('for_php7.php');

require_once('knjm443wModel.inc');
require_once('knjm443wQuery.inc');

class knjm443wController extends Controller {
    var $ModelClassName = "knjm443wModel";
    var $ProgramID      = "KNJM443W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm443wForm1");
                    }
                    break 2;
                case "":
                case "knjm443w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm443wModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm443wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm443wCtl = new knjm443wController;
//var_dump($_REQUEST);
?>

