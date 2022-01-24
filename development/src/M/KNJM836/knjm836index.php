<?php

require_once('for_php7.php');

require_once('knjm836Model.inc');
require_once('knjm836Query.inc');

class knjm836Controller extends Controller {
    var $ModelClassName = "knjm836Model";
    var $ProgramID      = "KNJM836";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm836Form1");
                    }
                    break 2;
                case "":
                case "knjm836":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm836Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm836Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm836Ctl = new knjm836Controller;
//var_dump($_REQUEST);
?>

