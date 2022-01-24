<?php

require_once('for_php7.php');

require_once('knjp853Model.inc');
require_once('knjp853Query.inc');

class knjp853Controller extends Controller
{
    public $ModelClassName = "knjp853Model";
    public $ProgramID      = "KNJP853";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":                            //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjp853Model();   //コントロールマスタの呼び出し
                    $this->callView("knjp853Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp853Ctl = new knjp853Controller();
