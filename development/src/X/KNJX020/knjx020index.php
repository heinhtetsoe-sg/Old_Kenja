<?php

require_once('for_php7.php');

require_once('knjx020Model.inc');
require_once('knjx020Query.inc');

class knjx020Controller extends Controller
{
    public $ModelClassName = "knjx020Model";
    public $ProgramID      = "KNJX020";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":            //データ取込
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv_data":        //データ出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDataModel()) {
                        $this->callView("knjx020Form1");
                    }
                    break 2;
                case "csv_error":       //エラー出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx020Form1");
                    }
                    break 2;
                case "csv_header":      //ヘッダ出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getHeaderModel()) {
                        $this->callView("knjx020Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjx020Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx020Ctl = new knjx020Controller();
