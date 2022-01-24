<?php

require_once('for_php7.php');

require_once('knjm503Model.inc');
require_once('knjm503Query.inc');

class knjm503Controller extends Controller {
    var $ModelClassName = "knjm503Model";
    var $ProgramID      = "KNJM503";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm503":                         //メニュー画面もしくはSUBMITした場合
                case "clschange":
                case "changeSem":
                    $sessionInstance->knjm503Model();   //コントロールマスタの呼び出し
                    $this->callView("knjm503Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm503Ctl = new knjm503Controller;
var_dump($_REQUEST);
?>
