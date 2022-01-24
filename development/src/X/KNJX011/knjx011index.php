<?php

require_once('for_php7.php');

require_once('knjx011Model.inc');
require_once('knjx011Query.inc');

class knjx011Controller extends Controller
{
    public $ModelClassName = "knjx011Model";
    public $ProgramID      = "KNJX011";

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
                        $this->callView("knjx011Form1");
                    }
                    break 2;
                case "csv_error":       //エラー出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx011Form1");
                    }
                    break 2;
                case "csv_header":      //ヘッダ出力
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getHeaderModel()) {
                        $this->callView("knjx011Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjx011Form1");
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
$knjx011Ctl = new knjx011Controller();
