<?php

require_once('for_php7.php');

require_once('knjz070_2Model.inc');
require_once('knjz070_2Query.inc');

class knjz070_2Controller extends Controller
{
    public $ModelClassName = "knjz070_2Model";
    public $ProgramID      = "KNJZ070";
    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ070_2");
                    $this->callView("knjz070_2Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ070_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ070_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "list":
                case "changeCmb":
                    $this->callView("knjz070_2Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ070_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz070_2index.php?cmd=list&year_code=".VARS::request("year_code");
                    $args["right_src"] = "knjz070_2index.php?cmd=edit&year_code=".VARS::request("year_code");
                    $args["cols"] = "50%,50%";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz070_2Ctl = new knjz070_2Controller();
//var_dump($_REQUEST);
