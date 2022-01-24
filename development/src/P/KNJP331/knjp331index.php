<?php

require_once('for_php7.php');

require_once('knjp331Model.inc');
require_once('knjp331Query.inc');

class knjp331Controller extends Controller {
    var $ModelClassName = "knjp331Model";
    var $ProgramID      = "KNJP331";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp331");
                    }
                    break 2;
                case "":
                case "knjp331":                             //メニュー画面もしくはSUBMITした場合
                case "knjp331ken":                          //NO003
                    $sessionInstance->knjp331Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp331Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp331Ctl = new knjp331Controller;
//var_dump($_REQUEST);
?>
