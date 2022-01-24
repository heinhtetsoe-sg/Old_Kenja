<?php

require_once('for_php7.php');

require_once('knjm838Model.inc');
require_once('knjm838Query.inc');

class knjm838Controller extends Controller {
    var $ModelClassName = "knjm838Model";
    var $ProgramID      = "KNJM838";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm838Form1");
                    }
                    break 2;
                case "":
                case "knjm838":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm838Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm838Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm838Ctl = new knjm838Controller;
//var_dump($_REQUEST);
?>

