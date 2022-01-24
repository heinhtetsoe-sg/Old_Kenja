<?php

require_once('for_php7.php');

require_once('knjd295Model.inc');
require_once('knjd295Query.inc');

class knjd295Controller extends Controller {
    var $ModelClassName = "knjd295Model";
    var $ProgramID      = "KNJD295";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "dtGrade":
                case "knjd295":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd295Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd295Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd295Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd295Ctl = new knjd295Controller;
//var_dump($_REQUEST);
?>
