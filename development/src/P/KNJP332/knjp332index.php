<?php

require_once('for_php7.php');

require_once('knjp332Model.inc');
require_once('knjp332Query.inc');

class knjp332Controller extends Controller {
    var $ModelClassName = "knjp332Model";
    var $ProgramID      = "KNJP332";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjp332Form1");
                    }
                    break 2;
                case "":
                case "knjp332":                             //メニュー画面もしくはSUBMITした場合
                case "knjp332ken":                          //NO003
                    $sessionInstance->knjp332Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp332Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp332Ctl = new knjp332Controller;
//var_dump($_REQUEST);
?>
