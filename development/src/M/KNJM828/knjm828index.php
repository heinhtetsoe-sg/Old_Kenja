<?php

require_once('for_php7.php');

require_once('knjm828Model.inc');
require_once('knjm828Query.inc');

class knjm828Controller extends Controller {
    var $ModelClassName = "knjm828Model";
    var $ProgramID      = "KNJM828";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm828Form1");
                    }
                    break 2;
                case "":
                case "knjm828":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm828Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm828Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm828Ctl = new knjm828Controller;
//var_dump($_REQUEST);
?>

