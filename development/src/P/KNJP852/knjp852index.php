<?php

require_once('for_php7.php');

require_once('knjp852Model.inc');
require_once('knjp852Query.inc');

class knjp852Controller extends Controller
{
    public $ModelClassName = "knjp852Model";
    public $ProgramID      = "KNJP852";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp852":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjp852Model();        //コントロールマスタの呼び出し
                    $this->callView("knjp852Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp852Ctl = new knjp852Controller();
//var_dump($_REQUEST);
