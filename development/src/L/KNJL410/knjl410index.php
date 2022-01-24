<?php

require_once('for_php7.php');


require_once('knjl410Model.inc');
require_once('knjl410Query.inc');

class knjl410Controller extends Controller
{
    public $ModelClassName = "knjl410Model";
    public $ProgramID      = "KNJL410";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeYear":
                case "changeKind":
                case "kousinZumi":
                case "ret411":
                case "ret410_1":
                case "ret410_2":
                case "reset":
                case "fuban":
                case "fubanClear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl410Form1");
                    break 2;
                case "add":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "update":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "delete":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "sendDel":
                    $this->checkAuth2(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getSendDeleteModel();
                    $sessionInstance->setCmd("kousinZumi");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = REQUESTROOT ."/L/KNJL_EVENT_SEARCH/index.php?";
                    $args["left_src"] .= "PROGRAMID=".$this->ProgramID;
                    $args["left_src"] .= "&AUTH=".AUTHORITY;
                    $args["left_src"] .= "&schoolKind=1";
                    $args["left_src"] .= "&recruit_no=1";
                    $args["left_src"] .= "&name=1";
                    $args["left_src"] .= "&name_kana=1";
                    $args["left_src"] .= "&event_class_cd=1";
                    $args["left_src"] .= "&event_cd=1";
                    $args["left_src"] .= "&media_cd=1";
                    $args["left_src"] .= "&finschoolcd=1";
                    $args["left_src"] .= "&grade=1";
                    $args["left_src"] .= "&prischoolcd=1";
                    $args["left_src"] .= "&prischoolClassCd=1";
                    $args["left_src"] .= "&SEND_NAME=".$sessionInstance->name;
                    $args["left_src"] .= "&SEND_KANA=".$sessionInstance->kana;
                    $args["left_src"] .= "&SEND_FINSCHOOLCD=".$sessionInstance->finschoolcd;
                    $args["left_src"] .= "&TARGET=right_frame";
                    $args["left_src"] .= "&PATH=" .urlencode("/L/KNJL410/knjl410index.php?cmd=main");
                    $args["right_src"] = "knjl410index.php?cmd=main";
                    $args["cols"] = "22%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
    
        //権限チェック
    public function checkAuth2($auth, $view = "")
    {
        $model =& Model::getModel($this);

        if ($model->auth < $auth) {
            $model->setWarning("MSG300");
            if ($view == "") {
                $view = $model->_view;
            }
            //直前に表示されたフォームを表示
            $this->callView($view);
            exit;
        }
    }
}
$knjl410Ctl = new knjl410Controller;
?>
