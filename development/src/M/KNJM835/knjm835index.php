<?php

require_once('for_php7.php');

require_once('knjm835Model.inc');
require_once('knjm835Query.inc');

class knjm835Controller extends Controller {
    var $ModelClassName = "knjm835Model";
    var $ProgramID      = "KNJM835";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm835Form1");
                    }
                    break 2;
                case "":
                case "knjm835":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm835Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm835Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm835Ctl = new knjm835Controller;
//var_dump($_REQUEST);
?>

