<?php

require_once('for_php7.php');

require_once('knjp330Model.inc');
require_once('knjp330Query.inc');

class knjp330Controller extends Controller {
    var $ModelClassName = "knjp330Model";
    var $ProgramID      = "KNJP330";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp330");
                    }
                    break 2;
                case "":
                case "knjp330":                             //メニュー画面もしくはSUBMITした場合
                case "knjp330ken":                          //NO003
                    $sessionInstance->knjp330Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp330Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp330Ctl = new knjp330Controller;
//var_dump($_REQUEST);
?>
