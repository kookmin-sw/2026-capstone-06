import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../../components/ui/card";
import { Button } from "../../components/ui/button";
import { Badge } from "../../components/ui/badge";
import { 
  Play, 
  Pause, 
  Volume2, 
  Clock,
  Calendar,
  Mic,
  Send
} from "lucide-react";
import { toast } from "sonner";

interface AudioEvent {
  id: string;
  timestamp: string;
  date: string;
  time: string;
  duration: number;
  audioUrl: string;
  analyzed: boolean;
  severity: 'low' | 'medium' | 'high';
  notes?: string;
}

export function AudioManagement() {
  const [playingId, setPlayingId] = useState<string | null>(null);
  const [recordingOwnerVoice, setRecordingOwnerVoice] = useState(false);
  const [audioEvents, setAudioEvents] = useState<AudioEvent[]>([]);

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL || '/api'}/audio/events`);
        const data = await response.json();
        setAudioEvents(data.content || []);
      } catch (error) {
        console.error('[Audio] Fetch Error:', error);
      }
    };
    fetchEvents();
  }, []);

  const handlePlayPause = (id: string) => {
    if (playingId === id) {
      setPlayingId(null);
      toast.info("재생 중지");
    } else {
      setPlayingId(id);
      toast.success("오디오 재생 중...");
    }
  };

  const handleSendOwnerVoice = () => {
    setRecordingOwnerVoice(true);
    toast.success("주인 음성 전송 중...");
    setTimeout(() => {
      setRecordingOwnerVoice(false);
      toast.success("음성이 펫하우스로 전송되었습니다");
    }, 2000);
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'high':
        return 'bg-red-100 text-red-700 border-red-300';
      case 'medium':
        return 'bg-yellow-100 text-yellow-700 border-yellow-300';
      case 'low':
        return 'bg-green-100 text-green-700 border-green-300';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-300';
    }
  };

  const getSeverityLabel = (severity: string) => {
    switch (severity) {
      case 'high':
        return '높음';
      case 'medium':
        return '보통';
      case 'low':
        return '낮음';
      default:
        return '미상';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold text-gray-900">음성 데이터 관리</h2>
          <p className="text-gray-500 mt-2">반려동물의 짖음 이벤트를 확인하고 관리하세요</p>
        </div>
        <Button 
          onClick={handleSendOwnerVoice}
          disabled={recordingOwnerVoice}
          className="bg-linear-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
        >
          <Send className="w-4 h-4 mr-2" />
          {recordingOwnerVoice ? "전송 중..." : "주인 음성 전송"}
        </Button>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold text-gray-900">24</div>
            <div className="text-sm text-gray-500 mt-1">오늘 짖음 횟수</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold text-gray-900">156</div>
            <div className="text-sm text-gray-500 mt-1">이번 주 총 횟수</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold text-gray-900">3.2분</div>
            <div className="text-sm text-gray-500 mt-1">평균 지속 시간</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <div className="text-2xl font-bold text-red-600">5</div>
            <div className="text-sm text-gray-500 mt-1">높은 심각도</div>
          </CardContent>
        </Card>
      </div>

      {/* Audio Events List */}
      <Card>
        <CardHeader>
          <CardTitle>짖음 이벤트 기록</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {audioEvents.map((event) => (
              <div 
                key={event.id}
                className="flex flex-col sm:flex-row sm:items-center gap-4 p-4 bg-gray-50 rounded-lg border border-gray-200 hover:border-blue-300 transition-colors"
              >
                {/* Play Button */}
                <Button
                  variant="outline"
                  size="icon"
                  onClick={() => handlePlayPause(event.id)}
                  className={playingId === event.id ? "bg-blue-50 border-blue-300" : ""}
                >
                  {playingId === event.id ? (
                    <Pause className="w-4 h-4" />
                  ) : (
                    <Play className="w-4 h-4" />
                  )}
                </Button>

                {/* Event Info */}
                <div className="flex-1 space-y-2">
                  <div className="flex flex-wrap items-center gap-2">
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                      <Calendar className="w-4 h-4" />
                      <span>{event.date}</span>
                    </div>
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                      <Clock className="w-4 h-4" />
                      <span>{event.time}</span>
                    </div>
                    <div className="flex items-center gap-1 text-sm text-gray-600">
                      <Volume2 className="w-4 h-4" />
                      <span>{event.duration}초</span>
                    </div>
                    <Badge className={getSeverityColor(event.severity)}>
                      심각도: {getSeverityLabel(event.severity)}
                    </Badge>
                    {event.analyzed && (
                      <Badge variant="secondary" className="bg-blue-100 text-blue-700">
                        <Mic className="w-3 h-3 mr-1" />
                        AI 분석 완료
                      </Badge>
                    )}
                  </div>
                  
                  {event.notes && (
                    <div className="text-sm text-gray-700 bg-white p-3 rounded border border-gray-200">
                      <span className="font-medium">분석 결과:</span> {event.notes}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Owner Voice Control */}
      <Card className="bg-linear-to-br from-blue-50 to-purple-50 border-blue-200">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Mic className="w-5 h-5 text-blue-600" />
            주인 음성 전달 기능
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-gray-700 mb-4">
            반려동물이 불안해할 때 주인의 음성을 펫하우스 스피커로 전달하여 안정시킬 수 있습니다.
          </p>
          <div className="flex gap-2">
            <Button 
              onClick={handleSendOwnerVoice}
              disabled={recordingOwnerVoice}
              className="bg-blue-600 hover:bg-blue-700"
            >
              <Send className="w-4 h-4 mr-2" />
              {recordingOwnerVoice ? "전송 중..." : "음성 전송하기"}
            </Button>
            <Button variant="outline">
              <Mic className="w-4 h-4 mr-2" />
              사전 녹음 관리
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
