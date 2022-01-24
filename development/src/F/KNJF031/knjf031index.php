<?php

require_once('for_php7.php');

require_once('knjf031Model.inc');
require_once('knjf031Query.inc');

class knjf031Controller extends Controller {
    var $ModelClassName = "knjf031Model";
    var $ProgramID        = "KNJF031";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf031":								//メニュー画面もしくはSUBMITした場合
                case "change_class":						//クラス変更時のSUBMITした場合
					$sessionInstance->knjf031Model();		//コントロールマスタの呼び出し
                    $this->callView("knjf031Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjf031Ctl = new knjf031Controller;
var_dump($_REQUEST);
?>
