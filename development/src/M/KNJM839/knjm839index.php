<?php

require_once('for_php7.php');

require_once('knjm839Model.inc');
require_once('knjm839Query.inc');

class knjm839Controller extends Controller {
    var $ModelClassName = "knjm839Model";
    var $ProgramID      = "KNJM839";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm839Form1");
                    }
                    break 2;
                case "":
                case "knjm839":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm839Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm839Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm839Ctl = new knjm839Controller;
//var_dump($_REQUEST);
?>

