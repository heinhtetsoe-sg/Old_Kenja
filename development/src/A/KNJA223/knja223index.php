<?php

require_once('for_php7.php');

require_once('knja223Model.inc');
require_once('knja223Query.inc');

class knja223Controller extends Controller {
    var $ModelClassName = "knja223Model";
    var $ProgramID      = "KNJA223";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja223":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja223Model();        //コントロールマスタの呼び出し
                    $this->callView("knja223Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knja223Form1");
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
$knja223Ctl = new knja223Controller;
//var_dump($_REQUEST);
?>
