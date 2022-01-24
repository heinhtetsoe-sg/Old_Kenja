<?php

require_once('for_php7.php');

require_once('knjm837Model.inc');
require_once('knjm837Query.inc');

class knjm837Controller extends Controller {
    var $ModelClassName = "knjm837Model";
    var $ProgramID      = "KNJM837";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm837Form1");
                    }
                    break 2;
                case "":
                case "knjm837":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm837Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm837Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm837Ctl = new knjm837Controller;
//var_dump($_REQUEST);
?>
