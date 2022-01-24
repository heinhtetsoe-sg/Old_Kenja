<?php

require_once('for_php7.php');

require_once('knjp721Model.inc');
require_once('knjp721Query.inc');

class knjp721Controller extends Controller
{
    public $ModelClassName = "knjp721Model";
    public $ProgramID      = "KNJP721";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "send":
                    $sessionInstance->getSendModel();
                    break 2;
                /* Tip Message 取得用Model */
                //異動情報取得
                case "sendT":
                    $sessionInstance->getSendModelTransfer();
                    break 2;
                //奨学金情報取得
                case "sendG":
                    $sessionInstance->getSendModelGenmen();
                    break 2;
                case "cmdStart":
                case "maingrade":
                case "mainclass":
                case "main":
                case "cancel":
                case "reSize":
                case "calc":
                    $sessionInstance->getMainModel();
                    $this->callView("knjp721Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjp721Form1");
                    }
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp721Ctl = new knjp721Controller();
