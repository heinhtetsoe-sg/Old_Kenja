<?php

require_once('for_php7.php');

require_once('knjm832Model.inc');
require_once('knjm832Query.inc');

class knjm832Controller extends Controller {
    var $ModelClassName = "knjm832Model";
    var $ProgramID      = "KNJM832";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm832":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm832Model();        //コントロールマスタの呼び出し
                    $this->callView("knjm832Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm832Ctl = new knjm832Controller;
//var_dump($_REQUEST);
?>

